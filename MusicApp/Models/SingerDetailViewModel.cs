using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace MusicApp.Models
{
	public class SingerDetailViewModel
	{
		public int Ma_Ca_Si { get; set; }          // Mã ca sĩ
		public string Ten_Ca_Si { get; set; }      // Tên ca sĩ
		public string HinhAnh { get; set; }        // Hình ảnh ca sĩ
		public string HinhAnh2 { get; set; }        // Hình ảnh ca sĩ2
		public string Description { get; set; }    // Mô tả ca sĩ
		public List<SongViewModel> Songs { get; set; }
	}
}